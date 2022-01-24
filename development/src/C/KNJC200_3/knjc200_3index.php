<?php

require_once('for_php7.php');

require_once('knjc200_3Model.inc');
require_once('knjc200_3Query.inc');

class knjc200_3Controller extends Controller {
    var $ModelClassName = "knjc200_3Model";
    var $ProgramID      = "KNJC200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "delete":  //削除
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "":
                case "schno":
					$sessionInstance->knjc200_3Model();		//コントロールマスタの呼び出し
                    $this->callView("knjc200_3Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc200_3Ctl = new knjc200_3Controller;
//var_dump($_REQUEST);
?>
