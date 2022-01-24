<?php

require_once('for_php7.php');

require_once('knjp972Model.inc');
require_once('knjp972Query.inc');

class knjp972Controller extends Controller {
    var $ModelClassName = "knjp972Model";
    var $ProgramID      = "KNJP972";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp972":                            //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjp972Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp972Form1");
                    exit;
                case "csvOutput":
                    if (!$sessionInstance->getCsvOutputModel()){
                        $this->callView("knjp972Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp972Ctl = new knjp972Controller;
?>
