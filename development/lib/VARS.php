<?php

require_once('for_php7.php');


class VARS extends PEAR {

    function &get($name)
    {
        return $_GET[$name];
    }

    function &post($name)
    {
        return $_POST[$name];
    }

    function &cookie($name)
    {
        return $_COOKIE[$name];
    }

    function &server($name)
    {
        return $_SERVER[$name];
    }

    function &env($name)
    {
        return $_ENV[$name];
    }

    function &request($name)
    {
        return $_REQUEST[$name];
    }

    function &session($name)
    {
        return $_SESSION[$name];
    }
    function &file($name)
    {
        return $_FILES[$name];
    }
}
?>
