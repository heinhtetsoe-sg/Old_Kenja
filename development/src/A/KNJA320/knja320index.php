<?php

require_once('for_php7.php');

require_once('knja320Model.inc');
require_once('knja320Query.inc');

class knja320Controller extends Controller {
    var $ModelClassName = "knja320Model";
    var $ProgramID      = "KNJA320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja320":
                    $sessionInstance->knja320Model();
                    $this->callView("knja320Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knja320Form1");
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
$knja320Ctl = new knja320Controller;
//var_dump($_REQUEST);
?>
