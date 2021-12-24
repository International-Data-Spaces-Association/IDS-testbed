FROM ruby:3

WORKDIR /opt

#Rebuild if Gemfile changed
COPY Gemfile .
COPY Gemfile.lock .
RUN bundle install

COPY . .

EXPOSE 4567

CMD [ "ruby", "omejdn.rb" ]
