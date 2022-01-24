<?php

require_once('for_php7.php');

require_once('knjj144iModel.inc');
require_once('knjj144iQuery.inc');

class knjj144iController extends Controller {
    var $ModelClassName = "knjj144iModel";
    var $ProgramID      = "KNJJ144I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjj144iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144iForm1");
                    exit;
                case "knjj144i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjj144iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj144iCtl = new knjj144iController;
//var_dump($_REQUEST);
?>
