<?php

require_once('for_php7.php');

require_once('knja143eModel.inc');
require_once('knja143eQuery.inc');

class knja143eController extends Controller {
    var $ModelClassName = "knja143eModel";
    var $ProgramID      = "KNJA143E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143e":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143eModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143eCtl = new knja143eController;
//var_dump($_REQUEST);
?>

