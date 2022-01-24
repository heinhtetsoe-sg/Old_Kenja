<?php

require_once('for_php7.php');

require_once('knjh101aModel.inc');
require_once('knjh101aQuery.inc');

class knjh101aController extends Controller {
    var $ModelClassName = "knjh101aModel";
    var $ProgramID      = "KNJH101A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh101a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh101aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh101aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh101aCtl = new knjh101aController;
//var_dump($_REQUEST);
?>
