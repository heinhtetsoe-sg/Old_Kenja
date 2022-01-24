<?php

require_once('for_php7.php');

require_once('knjl312cModel.inc');
require_once('knjl312cQuery.inc');

class knjl312cController extends Controller {
    var $ModelClassName = "knjl312cModel";
    var $ProgramID      = "KNJL312C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl312cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl312cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl312cCtl = new knjl312cController;
//var_dump($_REQUEST);
?>
