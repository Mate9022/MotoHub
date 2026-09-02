import {
  Component,
  signal
} from '@angular/core';

import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';


@Component({
  selector: 'app-root',
  standalone: true,

  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],

  template: `

    <header class="app-header">

      <div class="app-header-inner">


        <!-- ============================================= -->
        <!-- MOTOHUB -->
        <!-- ============================================= -->

        <a
            routerLink="/work-orders"
            class="brand"
            (click)="closeMenu()">

          <span class="brand-logo">
            M
          </span>

          <span class="brand-content">

            <span class="brand-name">
              MotoHub
            </span>

            <span class="brand-subtitle">
              Műhelykezelő
            </span>

          </span>

        </a>


        <!-- ============================================= -->
        <!-- MOBIL MENÜ -->
        <!-- ============================================= -->

        <button
            class="mobile-menu-button"
            type="button"
            aria-label="Navigáció megnyitása"
            [attr.aria-expanded]="menuOpen()"
            (click)="toggleMenu()">

          {{
            menuOpen()
                ? '✕'
                : '☰'
          }}

        </button>


        <!-- ============================================= -->
        <!-- NAVIGÁCIÓ -->
        <!-- ============================================= -->

        <nav
            class="app-navigation"
            [class.open]="menuOpen()">


          <a
              routerLink="/work-orders"
              routerLinkActive="active"
              [routerLinkActiveOptions]="{
                exact: true
              }"
              (click)="closeMenu()">

            <span class="nav-icon">
              📋
            </span>

            Munkalapok

          </a>


          <a
              routerLink="/customers"
              routerLinkActive="active"
              (click)="closeMenu()">

            <span class="nav-icon">
              👤
            </span>

            Ügyfelek

          </a>


          <a
              routerLink="/motorcycles"
              routerLinkActive="active"
              (click)="closeMenu()">

            <span class="nav-icon">
              🏍️
            </span>

            Motorok

          </a>


          <a
              routerLink="/settings"
              routerLinkActive="active"
              (click)="closeMenu()">

            <span class="nav-icon">
              ⚙️
            </span>

            Beállítások

          </a>


          <a
              routerLink="/work-orders/new"
              routerLinkActive="active"
              class="new-work-order-link"
              (click)="closeMenu()">

            <span>
              ＋
            </span>

            Új munkalap

          </a>

        </nav>

      </div>

    </header>


    <main class="app-main">

      <router-outlet />

    </main>

  `
})
export class AppComponent {


  readonly menuOpen =
      signal(false);


  toggleMenu(): void {

    this.menuOpen.update(
        value =>
            !value
    );

  }


  closeMenu(): void {

    this.menuOpen.set(
        false
    );

  }

}