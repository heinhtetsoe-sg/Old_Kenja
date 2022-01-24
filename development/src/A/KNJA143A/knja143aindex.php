<?php

require_once('for_php7.php');

require_once('knja143aModel.inc');
require_once('knja143aQuery.inc');

class knja143aController extends Controller {
    var $ModelClassName = "knja143aModel";
    var $ProgramID      = "KNJA143A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "grade":
                case "knja143a":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja143aModel();		//コントロールマスタの呼び出し
                    $this->callView("knja143aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja143aCtl = new knja143aController;
//var_dump($_REQUEST);
?>
