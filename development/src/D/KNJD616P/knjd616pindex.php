<?php

require_once('for_php7.php');

require_once('knjd616pModel.inc');
require_once('knjd616pQuery.inc');

class knjd616pController extends Controller {
    var $ModelClassName = "knjd616pModel";
    var $ProgramID      = "KNJD616P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd616pModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616pForm1");
                    exit;
                case "knjd616pChseme":
                case "knjd616p":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd616pModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd616pCtl = new knjd616pController;
//var_dump($_REQUEST);
?>
