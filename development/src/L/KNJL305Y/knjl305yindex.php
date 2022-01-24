<?php

require_once('for_php7.php');

require_once('knjl305yModel.inc');
require_once('knjl305yQuery.inc');

class knjl305yController extends Controller {
    var $ModelClassName = "knjl305yModel";
    var $ProgramID      = "KNJL305Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305y":                            //メニュー画面もしくはSUBMITした場合
                case "read":                                //更新印刷
                    $sessionInstance->knjl305yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl305yForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl305yCtl = new knjl305yController;
?>
