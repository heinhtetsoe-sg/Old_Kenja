<?php
require_once('knjl307yModel.inc');
require_once('knjl307yQuery.inc');

class knjl307yController extends Controller {
    var $ModelClassName = "knjl307yModel";
    var $ProgramID      = "KNJL307Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl307yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl307yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl307yCtl = new knjl307yController;
?>
