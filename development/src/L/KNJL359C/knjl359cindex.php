<?php

require_once('for_php7.php');

require_once('knjl359cModel.inc');
require_once('knjl359cQuery.inc');

class knjl359cController extends Controller {
    var $ModelClassName = "knjl359cModel";
    var $ProgramID      = "KNJL359C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl359c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl359cModel();          //コントロールマスタの呼び出し
                    $this->callView("knjl359cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl359cCtl = new knjl359cController;
//var_dump($_REQUEST);
?>
