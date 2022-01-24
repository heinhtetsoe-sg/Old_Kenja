<?php

require_once('for_php7.php');

require_once('knjl325Model.inc');
require_once('knjl325Query.inc');

class knjl325Controller extends Controller {
    var $ModelClassName = "knjl325Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325":
                    $sessionInstance->knjl325Model();
                    $this->callView("knjl325Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl325Form1");
					}
					break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl325Ctl = new knjl325Controller;
//var_dump($_REQUEST);
?>
