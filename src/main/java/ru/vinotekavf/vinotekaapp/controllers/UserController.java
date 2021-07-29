package ru.vinotekavf.vinotekaapp.controllers;

import com.ibm.icu.text.Transliterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.entities.User;
import ru.vinotekavf.vinotekaapp.enums.Role;
import ru.vinotekavf.vinotekaapp.services.PositionService;
import ru.vinotekavf.vinotekaapp.services.ProviderService;
import ru.vinotekavf.vinotekaapp.services.StorageService;
import ru.vinotekavf.vinotekaapp.services.UserService;
import ru.vinotekavf.vinotekaapp.utils.ControllerUtils;
import ru.vinotekavf.vinotekaapp.utils.FileUtils;
import ru.vinotekavf.vinotekaapp.utils.MediaTypeUtils;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Controller
public class UserController {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private StorageService storageService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @GetMapping("/")
    public String main(Model model){
        model.addAttribute("providers", providerService.getAllActive());
        return "main";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/registration")
    public String registration(){
        return "registration";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/registration")
    public String addUser(
        @RequestParam("passwordConfirm") String passwordConfirm,
        @Valid User user,
        BindingResult bindingResult,
        Model model,
        @RequestParam(value = "isAdmin", required = false) String isAdmin
    ){
        boolean isEmpty = StringUtils.isEmpty(passwordConfirm);

        if(isEmpty) {
            model.addAttribute("password2Error", "Повторите пароль");
        }

        if(!StringUtils.isEmpty(user.getPassword()) && !user.getPassword().equals(passwordConfirm)) {
            model.addAttribute("passwordError", "Пароли не совпадают!");
        }
        if (isEmpty || bindingResult.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            return "registration";
        }
        if(!userService.addUser(user)) {
            model.addAttribute("usernameError", "Пользователь уже существует");
            return "registration";
        }

        if(StringUtils.isEmpty(isAdmin)){
            user.setRoles(Collections.singleton(Role.USER));
        } else {
            user.setRoles(Collections.singleton(Role.ADMIN));
        }

        userService.save(user);
        return "redirect:";
    }

    @GetMapping("/getPrice")
    public ResponseEntity<ByteArrayResource> getPrice() throws IOException {
        FileUtils fileUtils = new FileUtils();
        File file = fileUtils.writeAllToXLSXFile(providerService.getAllActive());
        storageService.uploadFile(file);
        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, "Common price.xlsx");

        byte[] data = storageService.dowloadFile("Common price.xlsx");
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
            // Content-Disposition
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Common price.xlsx")
            // Content-Type
            .contentType(mediaType) //
            // Content-Lengh
            .contentLength(data.length) //
            .body(resource);
    }

    @GetMapping("/providers/download/{provider}")
    public ResponseEntity<ByteArrayResource> downloadProviderFile(@PathVariable Provider provider) throws IOException {
        FileUtils fileUtils = new FileUtils();
        File file = fileUtils.writeSingleToXLSXFile(providerService.getProviderById(provider.getId()));
        storageService.uploadFile(file);

        Transliterator toLatinTrans = Transliterator.getInstance("Cyrillic-Latin");

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext,toLatinTrans.transliterate(provider.getName()) + ".xlsx");

        byte[] data = storageService.dowloadFile(toLatinTrans.transliterate(provider.getName()) + ".xlsx");
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
            // Content-Disposition
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + toLatinTrans.transliterate(provider.getName()) + ".xlsx")
            // Content-Type
            .contentType(mediaType) //
            // Content-Lengh
            .contentLength(data.length) //
            .body(resource);
    }
}
