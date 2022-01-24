<?php
require_once('knjl308yModel.inc');
require_once('knjl308yQuery.inc');

class knjl308yController extends Controller {
    var $ModelClassName = "knjl308yModel";
    var $ProgramID      = "KNJL308Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl308y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl308yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl308yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl308yCtl = new knjl308yController;
?>
