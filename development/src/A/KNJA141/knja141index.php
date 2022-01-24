<?php

require_once('for_php7.php');

require_once('knja141Model.inc');
require_once('knja141Query.inc');

class knja141Controller extends Controller {
    var $ModelClassName = "knja141Model";
    var $ProgramID      = "KNJA141";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class"://---2005.07.15
                case "output":
                case "knja141":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja141Model();		//コントロールマスタの呼び出し
                    $this->callView("knja141Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja141Ctl = new knja141Controller;
//var_dump($_REQUEST);
?>
