<?php
require_once('knjl306yModel.inc');
require_once('knjl306yQuery.inc');

class knjl306yController extends Controller {
    var $ModelClassName = "knjl306yModel";
    var $ProgramID      = "KNJL306Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl306yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl306yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl306yCtl = new knjl306yController;
?>
