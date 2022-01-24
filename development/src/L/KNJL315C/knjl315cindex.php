<?php

require_once('for_php7.php');

require_once('knjl315cModel.inc');
require_once('knjl315cQuery.inc');

class knjl315cController extends Controller
{
    public $ModelClassName = "knjl315cModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315c":
                    $sessionInstance->knjl315cModel();
                    $this->callView("knjl315cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl315cForm1");
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
$knjl315cCtl = new knjl315cController();
//var_dump($_REQUEST);
