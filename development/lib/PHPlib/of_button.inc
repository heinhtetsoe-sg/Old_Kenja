<?php

require_once('for_php7.php');

class of_button extends of_element {

  function of_button($a) {    
    $this->setup_element($a);
  }

  function self_get($val, $which, &$count) {
    $str = "";
    
    $sv = empty($val) ? $this->value : $val;
    $str .= "<input type='button' name='$this->name' value='$sv'";
    if ($this->extrahtml) 
      $str .= " $this->extrahtml";
    $str .= ">";
    
    $count = 1;
    return $str;
  }
  
  function self_load_defaults($val) {
    // SUBMIT will not change its value
  }
} // end BUTTON
?>
