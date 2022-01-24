<?php

require_once('for_php7.php');

require_once('knja143mModel.inc');
require_once('knja143mQuery.inc');

class knja143mController extends Controller {
    var $ModelClassName = "knja143mModel";
    var $ProgramID      = "KNJA143M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143mModel();      //コントロールマスタの呼び出し
                    $this->callView("knja143mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143mCtl = new knja143mController;
//var_dump($_REQUEST);
?>
