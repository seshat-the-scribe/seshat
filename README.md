seshat
======

Sehast is a data pipeline assembly library and program; it can be uses standalone or within your programs.

## Use Cases

It started as a replacement for logstash but it evolved into a general purpose pipeline framework.

So logs processing is one use case but any data ingestion, transform and forward use case should be able to be tackled by using this software.
Seshat also gives you distributed counters and durable channels with single and multi-consumer semantics as well as point-to-point and fan-out semantics.


## Concepts overview

The simples thing you can do is build a pipeline. It is composed by `Input`s, `Filter`s and `Output`s.
Inputs can consume or receive messages, Filters transform the messages one after the other and Outputs send this processed messages to another pipelines or external systems.
The internal communication between pipelines is done via Channels which are kind of a queues that con be setup flexibly with several configuration options.


## Configuration


## Pipelines in depth


## Channels in depth





