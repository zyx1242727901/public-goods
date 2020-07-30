package com.xh.publicgoods.controller;

import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.enums.ResultEnum;
import com.xh.publicgoods.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暂时都用requestMapping，免得凯锅页面请求方式写起来麻烦
 * @author zhangyuxiao
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    private RoomService roomService;

    /**
     * 游戏大厅页面数据
     */
    @RequestMapping("/getHallInfo")
    public JSONObject getHallInfo(){
        return roomService.getHallInfo();
    }

    /**
     * 创建房间
     */
    @RequestMapping("/createRoom")
    public JSONObject createRoom(){
        return roomService.createRoom();
    }

    /**
     * 结束游戏
     * @param roomId
     * @return
     */
    @RequestMapping("/{roomId}/finalizeGame")
    public JSONObject createRoom(@PathVariable("roomId") String roomId){
        return roomService.finalizeGame(roomId);
    }

}
