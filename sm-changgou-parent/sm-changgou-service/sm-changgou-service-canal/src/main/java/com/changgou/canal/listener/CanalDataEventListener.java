package com.changgou.canal.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import com.changgou.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/30 17:20
 * <p>
 * @Description：
 */
@CanalEventListener
public class CanalDataEventListener {


    //注入StringRedisTemplate
    @Autowired
    private StringRedisTemplate redisTemplate;
    //注入feign
    @Autowired
    private ContentFeign contentFeign;


    /***
     * 增加数据监听
     * @param eventType
     * @param rowData
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("列名：" + column.getName() + "----------变更的数据：" + column.getValue());
        }

    }


    /****
     * @InsertListenPoint:修改数据监听
     * rowData.getAfterColumnsList():增加、修改   之后
     * rowData.getBeforeColumnsList()：删除、修改  之前
     * @param eventType : 当前操作的类型  增加数据
     * @param rowData ： 发生变更的一行数据
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //变更之前
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("修改前：列名：" + column.getName() + "--------变更的数据:" + column.getValue());
        }
        //变更之后
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("修改后：列名：" + column.getName() + "--------变更的数据:" + column.getValue());
        }
    }

    /***
     * 删除数据监听
     * @param eventType
     * @param rowData
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //删除该条数据之前的数据
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("删除前列名：" + column.getName() + "--------变更的数据:" + column.getValue());
        }
    }

    /*****
     * 自定义 监听
     * @param eventType
     * @param rowData
     */
    @ListenPoint(eventType = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE},
            table = {"tb_content", "tb_content_category", "order_info"},
            destination = "example",
            schema = {"changgou_content", "fescar-order"}

    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("===自定义操作前：列名：" + column.getName() + "--------变更的数据:" + column.getValue());
        }

        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("===自定义操作后：列名：" + column.getName() + "--------变更的数据:" + column.getValue());
        }

    }

    /*********
     *  同步redis中广告数据
     * @param eventType
     * @param rowData
     */
    @ListenPoint(
            eventType = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE},
            schema = "changgou_content",
            table = "tb_content",
            destination = "example"
    )
    public void onEventCustomRedis(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //获取广告分类的id
        String categoryid = getColumn(rowData, "category_id");
        Result<List<Content>> result = contentFeign.findByCategory(Integer.valueOf(categoryid));

        //获取所有的广告数据
        List<Content> contents = result.getData();
        //将所有的广告数据 同步到redis中
        redisTemplate.boundValueOps("content_"+categoryid).set(JSON.toJSONString(contents));
    }

    /*************
     * 获取广告分类的id
     * @param rowData
     * @param category_id
     * @return
     */
    public String getColumn(CanalEntry.RowData rowData, String category_id) {

        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if (column.getName().equals(category_id)) {
                return column.getValue();
            }
        }
        //有可能是删除操作
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            if (column.getName().equals(category_id)) {
                return column.getValue();
            }
        }
        return null;
    }

}
