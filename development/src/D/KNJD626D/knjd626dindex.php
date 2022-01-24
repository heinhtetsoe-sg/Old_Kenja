<?php

require_once('for_php7.php');

require_once('knjd626dModel.inc');
require_once('knjd626dQuery.inc');

class knjd626dController extends Controller {
    var $ModelClassName = "knjd626dModel";
    var $ProgramID      = "KNJD626D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd626dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626dForm1");
                    exit;
                case "change_grade":
                case "knjd626d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd626dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd626dCtl = new knjd626dController;
//var_dump($_REQUEST);
?>
