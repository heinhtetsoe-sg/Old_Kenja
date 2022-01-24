<?php

require_once('for_php7.php');

require_once('knjg022Model.inc');
require_once('knjg022Query.inc');

class knjg022Controller extends Controller {
    var $ModelClassName = "knjg022Model";
    var $ProgramID      = "KNJG022";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg022":
                    $sessionInstance->knjg022Model();
                    $this->callView("knjg022Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjg022Ctl = new knjg022Controller;
//var_dump($_REQUEST);
?>
