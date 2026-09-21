-- 返回 1 表示首次请求，返回 0 表示重复请求。
if redis.call('EXISTS', KEYS[1]) == 1 then
    return 0
end
redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
return 1
