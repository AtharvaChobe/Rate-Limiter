local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local currentTime = tonumber(ARGV[3])

local tokens = tonumber(redis.call('HGET', key, 'tokens'))
local lastRefill = tonumber(redis.call('HGET', key, 'lastRefill'))

if tokens == nil then
    tokens = capacity
    lastRefill = currentTime
end

local elapsed = (currentTime - lastRefill) / 1000

local newTokens = elapsed * refillRate

tokens = math.min(capacity, tokens + newTokens)

local allowed = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call(
    'HSET',
    key,
    'tokens',
    tokens,
    'lastRefill',
    currentTime
)

return allowed