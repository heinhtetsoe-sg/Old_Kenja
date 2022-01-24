<?php

require_once('for_php7.php');

require_once('knjd626cModel.inc');
require_once('knjd626cQuery.inc');

class knjd626cController extends Controller {
    var $ModelClassName = "knjd626cModel";
    var $ProgramID      = "KNJD626C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd626cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626cForm1");
                    exit;
                case "change_grade":
                case "knjd626c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd626cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd626cCtl = new knjd626cController;
//var_dump($_REQUEST);
?>
