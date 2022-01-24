<?php
require_once('knjl327kModel.inc');
require_once('knjl327kQuery.inc');

class knjl327kController extends Controller {
    var $ModelClassName = "knjl327kModel";
    var $ProgramID      = "KNJL327K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl327kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl327kForm1");
                    exit;
                case "output":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjl327kForm1");
                    }
                    break 2;
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "main":
                    $this->callView("knjl327kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl327kCtl = new knjl327kController;
//var_dump($_REQUEST);
?>
