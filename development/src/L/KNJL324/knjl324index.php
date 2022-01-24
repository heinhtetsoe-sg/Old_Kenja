<?php

require_once('for_php7.php');

require_once('knjl324Model.inc');
require_once('knjl324Query.inc');

class knjl324Controller extends Controller {
    var $ModelClassName = "knjl324Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324":
                    $sessionInstance->knjl324Model();
                    $this->callView("knjl324Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl324Form1");
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
$knjl324Ctl = new knjl324Controller;
//var_dump($_REQUEST);
?>
