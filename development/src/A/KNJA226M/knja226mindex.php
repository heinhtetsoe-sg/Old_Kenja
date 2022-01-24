<?php

require_once('for_php7.php');

require_once('knja226mModel.inc');
require_once('knja226mQuery.inc');

class knja226mController extends Controller {
    var $ModelClassName = "knja226mModel";
    var $ProgramID      = "knja226m";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja226m":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja226mModel();       //コントロールマスタの呼び出し
                    $this->callView("knja226mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja226mCtl = new knja226mController;
//var_dump($_REQUEST);
?>
