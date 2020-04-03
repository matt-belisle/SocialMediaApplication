DROP PROCEDURE insert_tweet;
CREATE PROCEDURE insert_tweet(in tweet TEXT,
                              in user_id INTEGER)
begin
    declare v_tweet_id INTEGER default 0;

    insert into Tweet (userID, text, timestamp) values (user_id, tweet, NOW());
    -- this is asynchronous safe
    set v_tweet_id = last_insert_id();

    -- this is what will be "returned"
    select * from Tweet t where t.id = v_tweet_id;

end;


DROP PROCEDURE get_replies;
CREATE PROCEDURE get_replies(
    in tweetID INTEGER
)
BEGIN
    DECLARE curr int;
    DECLARE prev int;
    set curr = tweetID;

    create TEMPORARY table IF NOT EXISTS temp_table as (select * from Tweet where Tweet.id = tweetID);
    -- this way nothing is shared between runs
    truncate temp_table;
    -- this only gets parent tweets, as there is an n to 1 branching relationship and that is beyond my sql skills as of now
    set curr = tweetID;
    set prev = 0;
    while(curr <> 0)
        DO
            insert into temp_table
            select Tweet.*
            from Tweet
                     inner join Reply on Tweet.id = Reply.originalID
            where Reply.replyID = curr;
            set prev = curr;
            set curr = 0;
            select originalID into curr from Reply where replyID = prev;
        END WHILE;
    insert into temp_table (select Tweet.*
                            from Tweet
                            where id in (select replyID from Reply where originalID = tweetID));
    -- add the original tweet
    insert into temp_table select Tweet.* from Tweet where Tweet.id = tweetID;
    -- this is what will be "returned"
    select distinct * from temp_table t where t.id order by timestamp;
end;
