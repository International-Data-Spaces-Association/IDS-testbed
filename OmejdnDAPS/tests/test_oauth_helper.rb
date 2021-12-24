# frozen_string_literal: true

require_relative '../lib/oauth_helper'
require 'test/unit'

# Basic OAuth Tester
class TestOAuthHelper < Test::Unit::TestCase
  def test_pkce
    assert(OAuthHelper.validate_pkce('E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM',
                                     'dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk', 'S256'))
  end
end
