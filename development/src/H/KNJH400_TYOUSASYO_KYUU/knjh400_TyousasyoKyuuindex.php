<?php

require_once('for_php7.php');

require_once('knjh400_TyousasyoKyuuModel.inc');
require_once('knjh400_TyousasyoKyuuQuery.inc');

class knjh400_TyousasyoKyuuController extends Controller
{
    public $ModelClassName = "knjh400_TyousasyoKyuuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3":         //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                case "yomikomi":
                    $this->callView("knjh400_TyousasyoKyuuForm1");
                    break 2;
                case "reload2_ok":  //学習指導要録より読込(OK)
                case "reload2_cancel":  //学習指導要録より読込(キャンセル)
                case "reload4":         //通知書より読込
                case "reload5":         //通知書より読込
                case "torikomi3":
                case "torikomi4":
                case "form2_first":     //「出欠の～」の最初の呼出
                case "form2":           //出欠の～
                    $this->callView("knjh400_TyousasyoKyuuForm2");
                    break 2;
                case "form3_first":     //「成績参照」の最初の呼出
                case "form3":           //「成績参照」
                    $this->callView("knjh400_TyousasyoKyuuSubForm1");
                    break 2;
                case "form4_first":     //「指導要録参照」の最初の呼出
                case "form4":           //「指導要録参照」
                    $this->callView("knjh400_TyousasyoKyuuSubForm2");
                    break 2;
                case "form6_first":     //「指導要録参照」の最初の呼出
                case "form6":           //「指導要録参照」
                    $this->callView("knjh400_TyousasyoKyuuSubForm6");
                    break 2;
                case "reload":          //保健より読み込み
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "reset":
                    $this->callView("knjh400_TyousasyoKyuuForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    $this->callView("knjh400_TyousasyoKyuuForm1");
                    break 2;
                case "back":
                    $programpath = $sessionInstance->getProgrampathModel();
                    $this->callView("knjh400_TyousasyoKyuuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_TyousasyoKyuuCtl = new knjh400_TyousasyoKyuuController();
//var_dump($_REQUEST);
