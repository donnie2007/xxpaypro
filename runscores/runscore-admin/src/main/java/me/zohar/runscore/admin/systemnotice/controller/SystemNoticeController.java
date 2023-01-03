package me.zohar.runscore.admin.systemnotice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.systemnotice.param.AddOrUpdateSystemNoticeParam;
import me.zohar.runscore.systemnotice.param.SystemNoticeQueryCondParam;
import me.zohar.runscore.systemnotice.service.SystemNoticeService;

@Controller
@RequestMapping("/systemNotice")
public class SystemNoticeController {

	@Autowired
	private SystemNoticeService systemNoticeService;

	@GetMapping("/findSystemNoticeById")
	@ResponseBody
	public Result findSystemNoticeById(String id) {
		return Result.success().setData(systemNoticeService.findSystemNoticeById(id));
	}

	@GetMapping("/delSystemNoticeById")
	@ResponseBody
	public Result delSystemNoticeById(String id) {
		systemNoticeService.delSystemNoticeById(id);
		return Result.success();
	}

	@PostMapping("/addOrUpdateSystemNotice")
	@ResponseBody
	public Result addOrUpdateSystemNotice(AddOrUpdateSystemNoticeParam param) {
		systemNoticeService.addOrUpdateSystemNotice(param);
		return Result.success();
	}

	@GetMapping("/findSystemNoticeByPage")
	@ResponseBody
	public Result findSystemNoticeByPage(SystemNoticeQueryCondParam param) {
		return Result.success().setData(systemNoticeService.findSystemNoticeByPage(param));
	}

}
