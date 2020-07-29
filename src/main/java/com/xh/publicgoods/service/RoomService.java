package com.xh.publicgoods.service;

import com.alibaba.fastjson.JSONObject;

public interface RoomService {
    /**
     * 游戏大厅页面数据
     */
    JSONObject getHallInfo();

    JSONObject createRoom();

    JSONObject destroyRoom(String roomId);
}
