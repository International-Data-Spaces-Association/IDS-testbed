# frozen_string_literal: true

require_relative './config'

require 'require_all'
begin
  require_rel 'claim_mappers'
rescue RequireAll::LoadError
  p 'No claim mappers available'
end

# Claim mapper plugin loader class
class ClaimMapper
  def self.map_claims(claims, provider)
    mname = "map_claims_#{provider['claim_mapper']}"
    unless ClaimMapper.singleton_methods.include?(mname.to_sym)
      p "No such claim mapper #{provider['claim_mapper']} in #{ClaimMapper.singleton_methods}"
    end
    return [] unless ClaimMapper.singleton_methods.include?(mname.to_sym)

    public_send("map_claims_#{provider['claim_mapper']}", claims, provider)
  end
end
