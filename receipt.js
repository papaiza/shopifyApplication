// Render HTML with a receipt for an order
function generateReceiptHtml(order) {
  
  var p = "Payment info: ";
  p += getPaymentTypeInfo(order);
  
  var orderHeader = document.createElement("h1");
  orderHeader.appendChild(document.createTextNode("Order receipt details"));

  var productOrder = "Your order of "+ order.products.name + " has been received";
  var productName = document.createElement("p");
  productName.appendChild(document.createTextNode(productOrder));

  var paymentInfo = document.createElement("p");
  paymentInfo.appendChild(document.createTextNode(p));

  document.body.appendChild(orderHeader);
  document.body.appendChild(productName);
  document.body.appendChild(paymentInfo);

  if (order.payment_type != "free") {
    var amount = "was charged $" + order.amount_in_dollars; 
    var payNotFree = document.createElement("p");
    payNotFree.appendChild(document.createTextNode(amount));
    document.body.appendChild(payNotFree);
  }

}
//Return payment type information
function getPaymentTypeInfo(order){
  switch (order.payment_type){
    case "creditcard" :
      return order.payment.getCardType + " " + maskCreditCard(order.payment.card_number);
    case "paypal" :
      return  order.payment.paypal_info;
    case "manual":
      return order.payment.manual_payment_info;
    case "free": 
      return "This order was free!" ;
    default:
      return order.payment.default_payment_info;
  }     
}

//Mask every number except the last 4 of a credit card.
function maskCreditCard(cardNumber){
    var len = cardNumber.length
    var maskedCC = "";
    for (i = 0; i < len - 5; i++){
        maskedCC += "*";
    }
    return maskedCC + cardNumber.substring(len - 4);
}





