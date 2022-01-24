<?php

require_once('for_php7.php');

require_once('knjj130Model.inc');
require_once('knjj130Query.inc');

class knjj130Controller extends Controller {
    var $ModelClassName = "knjj130Model";
    var $ProgramID      = "KNJJ130";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj130":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj130Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj130Form1");
                    exit;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj130Form1");
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
$knjj130Ctl = new knjj130Controller;
//var_dump($_REQUEST);
?>
