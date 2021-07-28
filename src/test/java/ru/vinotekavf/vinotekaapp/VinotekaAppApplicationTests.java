package ru.vinotekavf.vinotekaapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.vinotekavf.vinotekaapp.services.PositionService;

@SpringBootTest
class VinotekaAppApplicationTests {

	@Autowired
	private PositionService positionService;

	@Test
	void contextLoads() {
		//positionService.writeAllPositionsInCSV();
		positionService.writeAllPositionsInCSVByDTO();
	}

}
