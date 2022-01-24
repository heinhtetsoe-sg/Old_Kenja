<?php

require_once('for_php7.php');

require_once('knja210Model.inc');
require_once('knja210Query.inc');

class knja210Controller extends Controller {
    var $ModelClassName = "knja210Model";
    var $ProgramID      = "KNJA210";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "toukei":
                case "":
                    $this->callView("knja210Form1");
                    exit;                           //この１行追加しました。nakamoto 03/07/29
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja210Ctl = new knja210Controller;
//var_dump($_REQUEST);
?>
