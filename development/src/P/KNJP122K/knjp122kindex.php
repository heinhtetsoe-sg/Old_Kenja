<?php

require_once('for_php7.php');

require_once('knjp122kModel.inc');
require_once('knjp122kQuery.inc');

class knjp122kController extends Controller
{
    public $ModelClassName = "knjp122kModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //CSV取込
                    if (!$sessionInstance->getDownloadModel()) {
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("main");
                        break 1;
                    }
                    break 2;
                case "":
                case "main":
                case "knjp122k":
                    $sessionInstance->knjp122kModel();
                    $this->callView("knjp122kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp122kCtl = new knjp122kController();
