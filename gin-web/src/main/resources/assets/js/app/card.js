define(function (require) {
  "use strict";
  var React = require('react');

  var SuitMap = {
    Clubs: {clazz: 'black', unicode: '\u2663'},
    Diamonds: {clazz: 'red', unicode: '\u2666'},
    Hearts: {clazz: 'red', unicode: '\u2665'},
    Spades: {clazz: 'black', unicode: '\u2660'}
  };

  var Card = React.createClass({
    handleClick: function () {
      if (this.props.callback) {
        this.props.callback(this.props.card.abbreviation);
      }
    },

    render: function () {
      var clazz = 'card ' + (this.props.card ? SuitMap[this.props.card.suit].clazz : '');
      if (this.props.card && this.props.lastDraw == this.props.card.abbreviation) {
        clazz += ' last-draw';
      }
      if (!this.props.callback) {
        clazz += ' disabled';
      }
      var str = this.props.card ? SuitMap[this.props.card.suit].unicode + this.props.card.face : '--';
      return React.DOM.span({
        onClick: this.handleClick,
        className: clazz
      }, str);
    }
  });

  return {Card: React.createFactory(Card), SuitMap: SuitMap};
});
