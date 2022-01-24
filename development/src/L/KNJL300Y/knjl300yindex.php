<?php
require_once('knjl300yModel.inc');
require_once('knjl300yQuery.inc');

class knjl300yController extends Controller {
    var $ModelClassName = "knjl300yModel";
    var $ProgramID      = "KNJL300Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl300yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl300yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl300yCtl = new knjl300yController;
?>
