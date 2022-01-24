<?php

require_once('for_php7.php');

require_once('knjl363cModel.inc');
require_once('knjl363cQuery.inc');

class knjl363cController extends Controller {
    var $ModelClassName = "knjl363cModel";
    var $ProgramID      = "KNJL363C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl363c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl363cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl363cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl363cCtl = new knjl363cController;
//var_dump($_REQUEST);
?>
