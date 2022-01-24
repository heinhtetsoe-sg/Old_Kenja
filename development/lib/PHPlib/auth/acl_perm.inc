<?php

require_once('for_php7.php');

class ACL_Perm {

  var $classname = "ACL_Perm";

  ## Hash ("Name" => Permission-Bitmask)
  var $permissions = array ();

  ## Optional-Hash ("Resource" => "Container-Class-Name")
  var $containerlist = array();
  
  ## Choose a default container
  var $defaultcontainer = "ACL_CT_Sql";

  ##
  ## Permission code
  ##
  function check($p,$resource = '_default'){
    global $auth;
    
    if (! $this->have_perm($p,$resource)) {    
      if (! isset($auth->auth["perm"][$resource]) ) {
        $auth->auth["perm"][$resource] = "";
      }
      $this->perm_invalid($auth->auth["perm"][$resource], $p);
      exit();
    }
  }

  function have_perm($p,$resource = '_default'){
    global $auth;
    
    if (! isset($auth->auth["perm"][$resource]) ) {
      $auth->auth["perm"][$resource] = "";
    }
    $pageperm = preg_split("/,/", $p);
    $userperm = preg_split("/,/", $auth->auth["perm"][$resource]);
    
    list ($ok0, $pagebits) = $this->permsum($pageperm,$resource);
    list ($ok1, $userbits) = $this->permsum($userperm,$resource);

    $has_all = (($userbits & $pagebits) == $pagebits);
    if (!($has_all && $ok0 && $ok1) ) {
      return false;
    } else {
      return true;
    }
  }

  ##
  ## Permission helpers.
  ##
  function permsum($p,$resource = '_default'){
    global $auth;
    
    if (!is_array($p)) {
      return array(false, 0);
    }
    $perms = $this->getpermslist($resource);
    
    $r = 0;
    reset($p);
    while(list($key, $val) = each($p)) {
      if (!isset($perms[$val])) {
        return array(false, 0);
      }
      $r |= $perms[$val];
    }
    
    return array(true, $r);
  }

  function getpermslist($resource){
    if ($resource == "_default"){
      return $this->permissions;
    }
    if ($this->containerlist[$resource]){
      $that = new $this->containerlist[$resource];
    } else {
      $that = new $this->defaultcontainer;
    }
    return $that->permslist($resource);
  }

  ##
  ## Dummy Method. Must be overridden by user.
  ##
  function perm_invalid($does_have, $must_have) { 
    printf("Access denied.\n"); 
  }
}

class ACL_Auth extends Auth {

  var $aclperm_class = "ACL_Perm";

  function getuserperms($uid){
    $tperm = new $aclperm_class;
    
    if (is_array($tperm->containerlist) && count($tperm->containerlist) > 0){
      while(list($resource,$container) = each($tperm->containerlist)){
	$that =  new $container;
	$this->auth['perm'] = $that->userperms($uid);
	unset($that);
      }
    }
    $that = new $tperm->defaultcontainer;
    $this->auth['perm'] = $that->userperms($uid);
  }
  
}