<?php

require_once('for_php7.php');

require_once('knjl352cModel.inc');
require_once('knjl352cQuery.inc');

class knjl352cController extends Controller {
    var $ModelClassName = "knjl352cModel";
    var $ProgramID      = "KNJL352C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl352cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl352cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl352cCtl = new knjl352cController;
//var_dump($_REQUEST);
?>
