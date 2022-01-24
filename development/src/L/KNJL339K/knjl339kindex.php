<?php
require_once('knjl339kModel.inc');
require_once('knjl339kQuery.inc');

class knjl339kController extends Controller {
    var $ModelClassName = "knjl339kModel";
    var $ProgramID      = "KNJL339K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl339k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl339kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl339kForm1");
                    exit;
                case "output":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjl339kForm1");
                    }
                    break 2;
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "main":
                    $this->callView("knjl339kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl339kCtl = new knjl339kController;
//var_dump($_REQUEST);
?>
