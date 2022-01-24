<?php

require_once('for_php7.php');

require_once('knjl409yModel.inc');
require_once('knjl409yQuery.inc');

class knjl409yController extends Controller {
    var $ModelClassName = "knjl409yModel";
    var $ProgramID      = "KNJL409Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl409y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl409yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl409yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl409yCtl = new knjl409yController;
?>
