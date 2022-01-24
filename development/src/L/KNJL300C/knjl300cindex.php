<?php

require_once('for_php7.php');

require_once('knjl300cModel.inc');
require_once('knjl300cQuery.inc');

class knjl300cController extends Controller {
    var $ModelClassName = "knjl300cModel";
    var $ProgramID      = "KNJL300C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl300cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl300cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl300cCtl = new knjl300cController;
//var_dump($_REQUEST);
?>
