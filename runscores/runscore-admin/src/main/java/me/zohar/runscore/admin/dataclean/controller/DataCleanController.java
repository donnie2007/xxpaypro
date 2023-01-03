package me.zohar.runscore.admin.dataclean.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.dataclean.param.DataCleanParam;
import me.zohar.runscore.dataclean.service.DataCleanService;

@Controller
@RequestMapping("/dataClean")
public class DataCleanController {

	@Autowired
	private DataCleanService dataCleanService;

	@PostMapping("/dataClean")
	@ResponseBody
	public Result clean(@RequestBody DataCleanParam param) {
		dataCleanService.dataClean(param);
		return Result.success();
	}

}
