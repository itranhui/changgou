package com.changgou.user.dao;
import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:ranhui
 * @Description:User的Dao
 * @Date
 *****/
public interface UserMapper extends Mapper<User> {
    /**
     * 增加用户积分
     * @param username
     * @param points
     */
    @Update("UPDATE tb_user SET points=points+#{points} WHERE  username=#{username}")
    void addUserPoints(@Param("username") String username, @Param("points") Integer points);
}
