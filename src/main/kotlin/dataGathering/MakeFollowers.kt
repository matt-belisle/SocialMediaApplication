package dataGathering

import dataBase.DatabaseFactory
import dataBase.tables.FollowsTable
import dataBase.tables.User
import dataBase.tables.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import kotlin.random.Random

object MakeFollowers {
    fun MakeFollowers(){
        val users = mutableListOf<User>()
        runBlocking {
            DatabaseFactory.dbQuery { UserTable.selectAll().forEach { users.add( User(it)) } }
        }
        //just give a user somewhere between 10 and 100 followers
        users.forEach {curr ->
            val toFollow= mutableListOf<User>()
            for(i in 0..Random.nextInt(10,100) ){
                var user = users.get(Random.nextInt(0,users.size))
                while(user.id == curr.id || toFollow.contains(user)){
                    user = users.get(Random.nextInt(0,users.size))
                }
                toFollow.add(user)
            }
            runBlocking {
                DatabaseFactory.dbQuery {
                    FollowsTable.batchInsert(toFollow) {
                        this[FollowsTable.userIDFollowed] = curr.id
                        this[FollowsTable.userIDFollower] = it.id
                    }
                }
            }
        }
    }
}

fun main(){
    DatabaseFactory.init()
    MakeFollowers.MakeFollowers()
}